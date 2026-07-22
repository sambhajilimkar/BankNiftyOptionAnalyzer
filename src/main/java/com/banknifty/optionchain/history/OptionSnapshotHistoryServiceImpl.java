package com.banknifty.optionchain.history;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.banknifty.optionchain.model.OptionSnapshot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OptionSnapshotHistoryServiceImpl implements OptionSnapshotHistoryService {

	/**
	 * Keep last N snapshots.
	 */
	@Value("${option.snapshot.history.size:300}")
	private int maxSnapshots;

	/**
	 * Keep snapshots for last N minutes.
	 */
	@Value("${option.snapshot.history.minutes:60}")
	private int retentionMinutes;

	private final Deque<SnapshotHolder> history = new ArrayDeque<>();

	@Override
	public synchronized void save(OptionSnapshot snapshot) {

		if (snapshot == null) {
			return;
		}

		history.addLast(new SnapshotHolder(LocalDateTime.now(), snapshot));

		trim();

		log.debug("Snapshot stored. Total snapshots : {}", history.size());
	}

	@Override
	public synchronized Optional<OptionSnapshot> latest() {

		SnapshotHolder holder = history.peekLast();

		if (holder == null) {
			return Optional.empty();
		}

		return Optional.of(holder.snapshot());
	}

	@Override
	public synchronized Optional<OptionSnapshot> latestMatching(String underlying, java.time.LocalDate expiry) {
		if (underlying == null || expiry == null) {
			return Optional.empty();
		}
		return history.descendingIterator().hasNext() ? findLatestMatching(underlying, expiry) : Optional.empty();
	}

	private Optional<OptionSnapshot> findLatestMatching(String underlying, java.time.LocalDate expiry) {
		Iterator<SnapshotHolder> iterator = history.descendingIterator();
		while (iterator.hasNext()) {
			OptionSnapshot snapshot = iterator.next().snapshot();
			if (underlying.equalsIgnoreCase(snapshot.underlying()) && expiry.equals(snapshot.expiry())) {
				return Optional.of(snapshot);
			}
		}
		return Optional.empty();
	}

	@Override
	public synchronized Optional<OptionSnapshot> previous() {

		if (history.size() < 2) {
			return Optional.empty();
		}

		Iterator<SnapshotHolder> iterator = history.descendingIterator();

		iterator.next();

		return Optional.of(iterator.next().snapshot());
	}

	@Override
	public synchronized Optional<OptionSnapshot> beforeMinutes(int minutes) {

		LocalDateTime target = LocalDateTime.now().minusMinutes(minutes);

		SnapshotHolder candidate = null;

		Iterator<SnapshotHolder> iterator = history.descendingIterator();

		while (iterator.hasNext()) {

			SnapshotHolder holder = iterator.next();

			if (!holder.timestamp().isAfter(target)) {

				candidate = holder;
				break;
			}
		}

		return candidate == null ? Optional.empty() : Optional.of(candidate.snapshot());
	}

	@Override
	public synchronized void clear() {

		history.clear();
	}

	/**
	 * Remove old snapshots.
	 */
	private void trim() {

		while (history.size() > maxSnapshots) {

			history.removeFirst();
		}

		LocalDateTime cutoff = LocalDateTime.now().minusMinutes(retentionMinutes);

		while (!history.isEmpty()) {

			SnapshotHolder first = history.peekFirst();

			if (first.timestamp().isAfter(cutoff)) {
				break;
			}

			history.removeFirst();
		}
	}

	/**
	 * Internal holder.
	 */
	private record SnapshotHolder(

			LocalDateTime timestamp,

			OptionSnapshot snapshot

	) {
	}

}
