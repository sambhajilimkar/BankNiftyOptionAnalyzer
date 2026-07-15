package com.banknifty.optionchain.analysis;

import lombok.Builder;

@Builder
public record OIAnalysisResult(

		boolean longBuildUp,

		boolean shortBuildUp,

		boolean longUnwinding,

		boolean shortCovering,

		boolean callWriting,

		boolean putWriting,

		int supportStrike,

		int resistanceStrike

) {
}