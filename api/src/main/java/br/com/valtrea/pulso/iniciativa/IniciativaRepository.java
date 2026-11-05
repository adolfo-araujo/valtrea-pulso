package br.com.valtrea.pulso.iniciativa;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IniciativaRepository extends JpaRepository<Iniciativa, UUID> {

	/** As visíveis de um bairro, nas fases pedidas, das mais novas para as mais antigas. */
	@Query("select i from Iniciativa i where i.bairro.id = :bairro and i.oculta = false and i.fase in :fases "
			+ "order by i.criadoEm desc")
	List<Iniciativa> visiveisDoBairro(@Param("bairro") UUID bairroId, @Param("fases") Collection<Fase> fases,
			Limit limite);

	@Query("select count(i) from Iniciativa i where i.bairro.id = :bairro and i.oculta = false "
			+ "and i.fase in (br.com.valtrea.pulso.iniciativa.Fase.ABERTA, br.com.valtrea.pulso.iniciativa.Fase.EM_ANDAMENTO)")
	long contarAtivasDoBairro(@Param("bairro") UUID bairroId);

	List<Iniciativa> findByAutorIdOrderByCriadoEmDesc(UUID autorId);

	long countByAutorIdAndCriadoEmAfter(UUID autorId, Instant desde);

	@Query("select count(i) from Iniciativa i where i.autor.id = :autor and i.fase in "
			+ "(br.com.valtrea.pulso.iniciativa.Fase.ABERTA, br.com.valtrea.pulso.iniciativa.Fase.EM_ANDAMENTO)")
	long contarAtivasDoAutor(@Param("autor") UUID autorId);

	List<Iniciativa> findByOcultaTrueOrderByOcultaEmAsc();

	List<Iniciativa> findByOcultaTrueAndOcultaEmBefore(Instant limite);

	long countByOcultaTrue();

	long countByFase(Fase fase);

}
