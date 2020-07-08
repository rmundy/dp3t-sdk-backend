/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.backend.sdk.ws.util;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

public class ValidationUtils {
	private final int KEY_LENGTH_BYTES;
	private final Duration retentionPeriod;
	private final Long batchLength;

	public ValidationUtils(int keyLengthBytes, Duration retentionPeriod, Long batchLength) {
		this.KEY_LENGTH_BYTES = keyLengthBytes;
		this.retentionPeriod = retentionPeriod;
		this.batchLength = batchLength;
	}

	public boolean isValidBase64Key(String value) {
		try {
			byte[] key = Base64.getDecoder().decode(value);
			if (key.length != KEY_LENGTH_BYTES) {
				return false;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isDateInRange(OffsetDateTime timestamp) {
		if (timestamp.isAfter(Instant.now().atOffset(ZoneOffset.UTC))) {
			return false;
		}
		return !timestamp.isBefore(Instant.now().atOffset(ZoneOffset.UTC).minus(retentionPeriod));
	}

	/**
	 * Check if the given timestamp is a valid key date: Must be midnight utc.
	 * 
	 * @param keyDate
	 * @return
	 */
	public boolean isValidKeyDate(long keyDate) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(keyDate), ZoneOffset.UTC).toLocalTime().equals(LocalTime.MIDNIGHT);
	}

	public boolean isValidBatchReleaseTime(long batchReleaseTime) throws BadBatchReleaseTimeException {
		if (batchReleaseTime % batchLength != 0) {
			throw new BadBatchReleaseTimeException();
		}
		return this.isDateInRange(OffsetDateTime.ofInstant(Instant.ofEpochMilli(batchReleaseTime), ZoneOffset.UTC));
	}

	public class BadBatchReleaseTimeException extends Exception {

		private static final long serialVersionUID = 618376703047108588L;

	}

}