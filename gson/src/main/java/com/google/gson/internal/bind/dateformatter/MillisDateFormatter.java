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

import java.util.Date;

public final class MillisDateFormatter implements DateFormatter
{
private MillisDateFormatter(){};
	
	private final static MillisDateFormatter INSTANCE = new MillisDateFormatter();

	@Override
	public String format(Date date) {
		return Long.toString(date.getTime());
	}

	@Override
	public Date parse(String dateAsString) {
		Long seconds = Long.parseLong(dateAsString);
		return new Date(seconds);
	}
	
	public static MillisDateFormatter getInstance()
	{
		return INSTANCE;
	}
	
}