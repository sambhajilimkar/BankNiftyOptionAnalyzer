package com.banknifty.optionchain.history;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.banknifty.optionchain.model.OptionSnapshot;
import com.banknifty.optionchain.service.OptionSnapshotService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OptionSnapshotScheduler {

	private final OptionSnapshotService optionSnapshotService;

	private final OptionSnapshotHistoryService historyService;

	/**
	 * Capture complete option chain snapshot every minute.
	 *
	 * Cron can be changed from application.yml
	 */
	@Scheduled(cron = "${option.snapshot.cron:0 * * * * *}")
	public void captureSnapshot() {

		try {

			OptionSnapshot snapshot = optionSnapshotService.getLatestSnapshot();

			if (snapshot == null) {

				log.warn("Option snapshot is null.");

				return;
			}

			historyService.save(snapshot);

			log.info("Snapshot Captured : Spot={} ATM={} Calls={} Puts={}", snapshot.spotPrice(), snapshot.atmStrike(),
					snapshot.calls().size(), snapshot.puts().size());

		} catch (Exception ex) {

			log.error("Failed to capture option snapshot.", ex);
		}
	}

}