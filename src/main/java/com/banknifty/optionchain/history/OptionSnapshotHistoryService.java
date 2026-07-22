package com.banknifty.optionchain.history;

import java.util.Optional;
import java.time.LocalDate;

import com.banknifty.optionchain.model.OptionSnapshot;

public interface OptionSnapshotHistoryService {

	/**
	 * Store latest snapshot.
	 */
	void save(OptionSnapshot snapshot);

	/**
	 * Previous snapshot.
	 */
	Optional<OptionSnapshot> previous();

	/**
	 * Latest snapshot.
	 */
	Optional<OptionSnapshot> latest();

	/** Latest snapshot for exactly the requested underlying and expiry. */
	Optional<OptionSnapshot> latestMatching(String underlying, LocalDate expiry);

	/**
	 * Snapshot before given minutes.
	 */
	Optional<OptionSnapshot> beforeMinutes(int minutes);

	/**
	 * Remove all snapshots.
	 */
	void clear();

}
