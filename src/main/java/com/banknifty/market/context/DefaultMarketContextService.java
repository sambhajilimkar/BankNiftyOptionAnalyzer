package com.banknifty.market.context;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DefaultMarketContextService implements MarketContextService {

	private static final ZoneId INDIA_ZONE = ZoneId.of("Asia/Kolkata");

	private static final LocalTime MARKET_OPEN = LocalTime.of(9, 15);

	private static final LocalTime MARKET_CLOSE = LocalTime.of(15, 30);

	private static final LocalTime OPENING_VOLATILITY_END = LocalTime.of(9, 30);

	private static final LocalTime MORNING_SESSION_END = LocalTime.of(11, 30);

	private static final LocalTime AFTERNOON_SESSION_START = LocalTime.of(13, 30);

	private static final LocalTime LATE_SESSION_START = LocalTime.of(14, 45);

	private static final LocalTime NEW_ENTRY_CUTOFF = LocalTime.of(15, 15);

	@Override
	public MarketContext analyse() {

		ZonedDateTime now = ZonedDateTime.now(INDIA_ZONE);

		LocalDate date = now.toLocalDate();
		LocalTime time = now.toLocalTime();

		List<String> warnings = new ArrayList<>();

		boolean weekday = isWeekday(date);

		boolean marketHours = weekday && !time.isBefore(MARKET_OPEN) && time.isBefore(MARKET_CLOSE);

		boolean tradeAllowed = marketHours;

		int confidenceAdjustment = 0;
		int riskAdjustment = 0;

		String marketSession;

		/*
		 * ===================================================== NON-TRADING DAY
		 * =====================================================
		 */
		if (!weekday) {

			marketSession = "MARKET_CLOSED";

			tradeAllowed = false;

			confidenceAdjustment = -100;

			riskAdjustment = 100;

			warnings.add("Indian equity market closed - weekend");

			return buildContext(tradeAllowed, confidenceAdjustment, riskAdjustment, marketSession, warnings);
		}

		/*
		 * ===================================================== PRE-MARKET
		 * =====================================================
		 */
		if (time.isBefore(MARKET_OPEN)) {

			marketSession = "PRE_MARKET";

			tradeAllowed = false;

			confidenceAdjustment = -100;

			riskAdjustment = 100;

			warnings.add("Regular BankNifty trading session has not started");

			return buildContext(tradeAllowed, confidenceAdjustment, riskAdjustment, marketSession, warnings);
		}

		/*
		 * ===================================================== POST-MARKET
		 * =====================================================
		 */
		if (!time.isBefore(MARKET_CLOSE)) {

			marketSession = "MARKET_CLOSED";

			tradeAllowed = false;

			confidenceAdjustment = -100;

			riskAdjustment = 100;

			warnings.add("Regular BankNifty trading session has ended");

			return buildContext(tradeAllowed, confidenceAdjustment, riskAdjustment, marketSession, warnings);
		}

		/*
		 * ===================================================== OPENING SESSION
		 *
		 * First 15 minutes frequently contain:
		 *
		 * - overnight gap adjustment - abnormal spreads - high volatility - false
		 * breakouts
		 *
		 * We allow analysis but reduce confidence.
		 * =====================================================
		 */
		if (time.isBefore(OPENING_VOLATILITY_END)) {

			marketSession = "OPENING";

			confidenceAdjustment = -10;

			riskAdjustment = 15;

			warnings.add("Opening-session volatility - require stronger confirmation");

		}

		/*
		 * ===================================================== MORNING SESSION
		 *
		 * Generally the strongest intraday price-discovery period.
		 * =====================================================
		 */
		else if (time.isBefore(MORNING_SESSION_END)) {

			marketSession = "MORNING";

			confidenceAdjustment = 5;

			riskAdjustment = 0;

		}

		/*
		 * ===================================================== MIDDAY SESSION
		 *
		 * Often lower participation and more range-bound behaviour.
		 * =====================================================
		 */
		else if (time.isBefore(AFTERNOON_SESSION_START)) {

			marketSession = "MIDDAY";

			confidenceAdjustment = -5;

			riskAdjustment = 5;

			warnings.add("Midday session - momentum may be weaker");

		}

		/*
		 * ===================================================== AFTERNOON SESSION
		 * =====================================================
		 */
		else if (time.isBefore(LATE_SESSION_START)) {

			marketSession = "AFTERNOON";

			confidenceAdjustment = 0;

			riskAdjustment = 0;

		}

		/*
		 * ===================================================== LATE SESSION
		 *
		 * Intraday option trades become increasingly sensitive to theta, closing flows
		 * and sudden reversals. =====================================================
		 */
		else if (time.isBefore(NEW_ENTRY_CUTOFF)) {

			marketSession = "LATE_SESSION";

			confidenceAdjustment = -5;

			riskAdjustment = 10;

			warnings.add("Late session - reduced time available for target achievement");

		}

		/*
		 * ===================================================== NEW ENTRY CUTOFF
		 *
		 * Avoid fresh option-buying recommendations close to market close.
		 * =====================================================
		 */
		else {

			marketSession = "CLOSING";

			tradeAllowed = false;

			confidenceAdjustment = -25;

			riskAdjustment = 25;

			warnings.add("New intraday entries blocked after 15:15 IST");

		}

		MarketContext context = buildContext(tradeAllowed, confidenceAdjustment, riskAdjustment, marketSession,
				warnings);

		log.debug("Market context: session={}, tradeAllowed={}, confidenceAdjustment={}, riskAdjustment={}",
				context.marketSession(), context.tradeAllowed(), context.confidenceAdjustment(),
				context.riskAdjustment());

		return context;
	}

	private MarketContext buildContext(boolean tradeAllowed, int confidenceAdjustment, int riskAdjustment,
			String marketSession, List<String> warnings) {

		return MarketContext.builder()

				.tradeAllowed(tradeAllowed)

				.confidenceAdjustment(confidenceAdjustment)

				.riskAdjustment(riskAdjustment)

				.marketSession(marketSession)

				/*
				 * These require proper external/contextual data.
				 *
				 * Do NOT guess them from date/time.
				 */
				.expiryDay(false)

				.weeklyExpiry(false)

				.monthlyExpiry(false)

				.eventDay(false)

				.highVolatility(false)

				.globalBullish(false)

				.globalBearish(false)

				.warnings(List.copyOf(warnings))

				.build();
	}

	private boolean isWeekday(LocalDate date) {

		DayOfWeek day = date.getDayOfWeek();

		return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
	}
}