package tradeService;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeServiceRepository extends JpaRepository<TradeService, Long>{
	TradeService findByFromAndTo(String from, String to);

}