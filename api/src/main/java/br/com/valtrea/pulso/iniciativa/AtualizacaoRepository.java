package br.com.valtrea.pulso.iniciativa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AtualizacaoRepository extends JpaRepository<Atualizacao, UUID> {

	List<Atualizacao> findByIniciativaIdOrderByCriadoEmAsc(UUID iniciativaId);

	long countByIniciativaId(UUID iniciativaId);

}
