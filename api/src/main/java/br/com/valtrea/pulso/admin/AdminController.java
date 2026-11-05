package br.com.valtrea.pulso.admin;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.valtrea.pulso.usuario.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Painel de administração: só números agregados. Nenhum nome, e-mail ou ação individual sai daqui (LGPD, princípio da
 * necessidade). A moderação das iniciativas denunciadas fica em {@link ModeracaoController}.
 */
@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Administração")
public class AdminController {

	static final int SEMANAS = 8;

	public record Metricas(Instant geradoEm, Contas contas, Uso uso, List<Semana> cadastrosPorSemana) {
	}

	/** @param confirmadas com e-mail confirmado */
	public record Contas(long total, long confirmadas, long novas7Dias, long novas30Dias, long comSessaoAtiva) {
	}

	/**
	 * @param bairrosComIniciativas bairros com pelo menos uma iniciativa aberta ou em andamento
	 * @param ativas               abertas ou em andamento
	 * @param iniciativas30Dias    publicadas nos últimos 30 dias
	 * @param apoios30Dias         apoios dados nos últimos 30 dias (o número que mostra se o Pulso funciona)
	 * @param ocultas              esperando revisão por causa de denúncias
	 */
	public record Uso(long bairros, long bairrosComIniciativas, long iniciativas, long ativas, long resolvidas,
			long iniciativas30Dias, long apoios, long apoios30Dias, long ocultas) {
	}

	/** @param semana segunda-feira (UTC) da semana */
	public record Semana(LocalDate semana, long cadastros) {
	}

	private final JdbcTemplate jdbc;
	private final UsuarioRepository usuarios;
	private final Administradores administradores;

	public AdminController(JdbcTemplate jdbc, UsuarioRepository usuarios, Administradores administradores) {
		this.jdbc = jdbc;
		this.usuarios = usuarios;
		this.administradores = administradores;
	}

	@GetMapping("/metricas")
	@Transactional(readOnly = true)
	@Operation(summary = "Números de uso agregados (só para administradores)")
	public Metricas metricas(@AuthenticationPrincipal Jwt jwt) {
		administradores.exigir(usuarios, jwt);
		return calcular(Instant.now());
	}

	Metricas calcular(Instant agora) {
		Timestamp agoraTs = Timestamp.from(agora);
		Timestamp d7 = Timestamp.from(agora.minus(7, ChronoUnit.DAYS));
		Timestamp d30 = Timestamp.from(agora.minus(30, ChronoUnit.DAYS));

		Contas contas = jdbc.queryForObject("""
				SELECT count(*),
				       count(*) FILTER (WHERE email_confirmado_em IS NOT NULL),
				       count(*) FILTER (WHERE criado_em >= ?),
				       count(*) FILTER (WHERE criado_em >= ?),
				       (SELECT count(DISTINCT usuario_id) FROM sessoes WHERE revogado_em IS NULL AND expira_em > ?)
				  FROM usuarios""", (rs, i) -> new Contas(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getLong(4),
				rs.getLong(5)), d7, d30, agoraTs);

		Uso uso = jdbc.queryForObject("""
				SELECT (SELECT count(*) FROM bairros),
				       (SELECT count(DISTINCT bairro_id) FROM iniciativas
				         WHERE NOT oculta AND fase IN ('ABERTA', 'EM_ANDAMENTO')),
				       (SELECT count(*) FROM iniciativas),
				       (SELECT count(*) FROM iniciativas WHERE fase IN ('ABERTA', 'EM_ANDAMENTO')),
				       (SELECT count(*) FROM iniciativas WHERE fase = 'RESOLVIDA'),
				       (SELECT count(*) FROM iniciativas WHERE criado_em >= ?),
				       (SELECT count(*) FROM apoios),
				       (SELECT count(*) FROM apoios WHERE criado_em >= ?),
				       (SELECT count(*) FROM iniciativas WHERE oculta)""",
				(rs, i) -> new Uso(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getLong(4), rs.getLong(5),
						rs.getLong(6), rs.getLong(7), rs.getLong(8), rs.getLong(9)),
				d30, d30);

		return new Metricas(agora, contas, uso, cadastrosPorSemana(agora));
	}

	private List<Semana> cadastrosPorSemana(Instant agora) {
		LocalDate estaSemana = agora.atOffset(ZoneOffset.UTC).toLocalDate()
				.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate primeira = estaSemana.minusWeeks(SEMANAS - 1);
		Map<LocalDate, Long> porSemana = new HashMap<>();
		jdbc.query("""
				SELECT (date_trunc('week', criado_em AT TIME ZONE 'UTC'))::date AS semana, count(*)
				  FROM usuarios
				 WHERE criado_em >= ?
				 GROUP BY 1""", (RowCallbackHandler) rs -> {
			porSemana.put(rs.getObject(1, LocalDate.class), rs.getLong(2));
		}, Timestamp.from(primeira.atStartOfDay(ZoneOffset.UTC).toInstant()));
		List<Semana> semanas = new ArrayList<>(SEMANAS);
		for (int i = 0; i < SEMANAS; i++) {
			LocalDate semana = primeira.plusWeeks(i);
			semanas.add(new Semana(semana, porSemana.getOrDefault(semana, 0L)));
		}
		return semanas;
	}

}
