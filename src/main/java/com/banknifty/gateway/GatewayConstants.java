package com.banknifty.gateway;

/**
 * Common constants used by Gateway layer.
 */
public final class GatewayConstants {

	private GatewayConstants() {
	}

	/**
	 * Instrument prefixes.
	 */
	public static final String NSE = "NSE";
	public static final String NFO = "NFO";
	public static final String BSE = "BSE";
	public static final String MCX = "MCX";

	/**
	 * Common intervals.
	 */
	public static final String ONE_MINUTE = "minute";
	public static final String THREE_MINUTE = "3minute";
	public static final String FIVE_MINUTE = "5minute";
	public static final String TEN_MINUTE = "10minute";
	public static final String FIFTEEN_MINUTE = "15minute";
	public static final String THIRTY_MINUTE = "30minute";
	public static final String SIXTY_MINUTE = "60minute";
	public static final String DAY = "day";

	/**
	 * Index Names.
	 */
	public static final String NIFTY_BANK = "NIFTY BANK";
	public static final String NIFTY_50 = "NIFTY 50";
	public static final String FINNIFTY = "NIFTY FIN SERVICE";
	public static final String MIDCPNIFTY = "NIFTY MID SELECT";

	/**
	 * Exchange Instrument Names.
	 */
	public static final String NSE_NIFTY_BANK = "NSE:NIFTY BANK";
	public static final String NSE_NIFTY_50 = "NSE:NIFTY 50";

	/**
	 * Gateway Status.
	 */
	public static final String STATUS_CONNECTED = "CONNECTED";
	public static final String STATUS_DISCONNECTED = "DISCONNECTED";

}