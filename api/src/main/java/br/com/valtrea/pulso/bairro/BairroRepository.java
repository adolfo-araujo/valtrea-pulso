package br.com.valtrea.pulso.bairro;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BairroRepository extends JpaRepository<Bairro, UUID> {

	Optional<Bairro> findByChave(String chave);

	/** Busca pela chave (sem acentos): "sao" acha "São Paulo" e "São Judas". */
	@Query("select b from Bairro b where (:uf is null or b.uf = :uf) and b.chave like concat('%', :termo, '%') "
			+ "order by b.uf, b.cidade, b.nome")
	List<Bairro> buscar(@Param("uf") String uf, @Param("termo") String termo, Limit limite);

}
