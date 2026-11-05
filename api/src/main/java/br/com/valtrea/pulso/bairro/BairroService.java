package br.com.valtrea.pulso.bairro;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.valtrea.pulso.bairro.BairroDtos.BairroRequest;
import br.com.valtrea.pulso.bairro.BairroDtos.BairroResponse;
import br.com.valtrea.pulso.erro.NegocioException;
import br.com.valtrea.pulso.iniciativa.IniciativaRepository;

@Service
public class BairroService {

	static final Set<String> UFS = Set.of("AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG",
			"PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

	private final BairroRepository bairros;
	private final IniciativaRepository iniciativas;

	public BairroService(BairroRepository bairros, IniciativaRepository iniciativas) {
		this.bairros = bairros;
		this.iniciativas = iniciativas;
	}

	@Transactional(readOnly = true)
	public List<BairroResponse> buscar(String uf, String busca) {
		String estado = uf == null || uf.isBlank() ? null : uf.strip().toUpperCase(Locale.ROOT);
		String termo = busca == null ? "" : Bairro.normalizar(busca);
		return bairros.buscar(estado, termo, Limit.of(30)).stream().map(this::resposta).toList();
	}

	@Transactional(readOnly = true)
	public BairroResponse buscar(UUID id) {
		return resposta(existente(id));
	}

	public Bairro existente(UUID id) {
		return bairros.findById(id)
				.orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Bairro não encontrado."));
	}

	/** Devolve o bairro com o mesmo nome, se já existir; senão, cria. */
	@Transactional
	public Bairro encontrarOuCriar(BairroRequest pedido) {
		String uf = pedido.uf().strip().toUpperCase(Locale.ROOT);
		if (!UFS.contains(uf)) {
			throw new NegocioException(HttpStatus.BAD_REQUEST, "Estado inválido. Use a sigla, como SP ou RJ.");
		}
		String chave = Bairro.chave(pedido.nome(), pedido.cidade(), uf);
		if (chave.endsWith("|") || chave.contains("||")) {
			throw new NegocioException(HttpStatus.BAD_REQUEST, "Escreva o nome do bairro e da cidade com letras.");
		}
		return bairros.findByChave(chave).orElseGet(() -> bairros.save(new Bairro(pedido.nome(), pedido.cidade(), uf)));
	}

	public BairroResponse resposta(Bairro b) {
		return new BairroResponse(b.getId(), b.getNome(), b.getCidade(), b.getUf(),
				iniciativas.contarAtivasDoBairro(b.getId()));
	}

}
