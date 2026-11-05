package br.com.valtrea.pulso.iniciativa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DenunciaRepository extends JpaRepository<Denuncia, Denuncia.Chave> {

	long countByIdIniciativaId(UUID iniciativaId);

	List<Denuncia> findByIdIniciativaId(UUID iniciativaId);

	List<Denuncia> findByIdUsuarioId(UUID usuarioId);

	@Modifying
	@Query("delete from Denuncia d where d.id.iniciativaId = :iniciativa")
	void apagarDa(@Param("iniciativa") UUID iniciativaId);

}
