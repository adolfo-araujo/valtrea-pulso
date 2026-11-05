package br.com.valtrea.pulso.iniciativa;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.valtrea.pulso.bairro.BairroDtos.BairroResponse;
import br.com.valtrea.pulso.bairro.BairroService;
import br.com.valtrea.pulso.iniciativa.IniciativaDtos.IniciativaResponse;
import br.com.valtrea.pulso.iniciativa.IniciativaDtos.IniciativaResumo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Leitura sem conta: qualquer pessoa vê o que o bairro está pedindo. Nada aqui identifica alguém. */
@RestController
@RequestMapping("/api/v1/publico")
@Tag(name = "Público")
public class PublicoController {

	private final BairroService bairros;
	private final IniciativaService iniciativas;

	public PublicoController(BairroService bairros, IniciativaService iniciativas) {
		this.bairros = bairros;
		this.iniciativas = iniciativas;
	}

	@GetMapping("/bairros")
	@Operation(summary = "Procura bairros pelo nome, pela cidade ou pelos dois")
	public List<BairroResponse> buscar(@RequestParam(required = false) String uf,
			@RequestParam(required = false) String busca) {
		return bairros.buscar(uf, busca);
	}

	@GetMapping("/bairros/{id}")
	@Operation(summary = "Um bairro")
	public BairroResponse bairro(@PathVariable UUID id) {
		return bairros.buscar(id);
	}

	@GetMapping("/bairros/{id}/iniciativas")
	@Operation(summary = "Iniciativas do bairro (padrão: abertas e em andamento, mais recentes primeiro)")
	public List<IniciativaResumo> doBairro(@PathVariable UUID id, @RequestParam(required = false) Fase fase,
			@RequestParam(required = false) String ordem) {
		return iniciativas.doBairro(id, fase, ordem, null);
	}

	@GetMapping("/iniciativas/{id}")
	@Operation(summary = "Uma iniciativa, com as atualizações")
	public IniciativaResponse iniciativa(@PathVariable UUID id) {
		return iniciativas.detalhe(id, null);
	}

}
