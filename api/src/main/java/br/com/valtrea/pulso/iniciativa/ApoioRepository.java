package br.com.valtrea.pulso.iniciativa;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApoioRepository extends JpaRepository<Apoio, Apoio.Chave> {

	long countByIdIniciativaId(UUID iniciativaId);

	/** [iniciativaId, quantidade] de cada iniciativa da lista que tem apoios. */
	@Query("select a.id.iniciativaId, count(a) from Apoio a where a.id.iniciativaId in :ids group by a.id.iniciativaId")
	List<Object[]> contar(@Param("ids") Collection<UUID> iniciativaIds);

	@Query("select a.id.iniciativaId from Apoio a where a.id.usuarioId = :usuario and a.id.iniciativaId in :ids")
	List<UUID> apoiadasPor(@Param("usuario") UUID usuarioId, @Param("ids") Collection<UUID> iniciativaIds);

	List<Apoio> findByIdUsuarioId(UUID usuarioId);

}
