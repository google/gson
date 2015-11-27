/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.internal.bind.dateformatter;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public final class SimpleDateFormatter implements DateFormatter {
	private final DateFormat dateFormat;

	public SimpleDateFormatter(DateFormat dateFormat) {
		// Clone to prevent object from change
		this.dateFormat = (DateFormat) dateFormat.clone();
	}

	// These methods need to be synchronized since JDK DateFormat classes are not
	// thread-safe
	// See issue 162
	@Override
	public synchronized String format(Date date) {
		return dateFormat.format(date);
	}

	@Override
	public synchronized Date parse(String dateAsString) throws ParseException {
		return dateFormat.parse(dateAsString);
	}

	@Override
	public String toString() {
		return dateFormat.getClass().getSimpleName();
	}
}