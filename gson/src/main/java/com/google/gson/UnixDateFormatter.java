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

package com.google.gson;

import java.util.Date;

final class UnixDateFormatter implements DateFormatter
{
private UnixDateFormatter(){};
	
	private final static UnixDateFormatter INSTANCE = new UnixDateFormatter();

	@Override
	public String format(Date date) {
		return Long.toString(date.getTime() / 1000);
	}

	@Override
	public Date parse(String dateAsString) {
		Long seconds = Long.parseLong(dateAsString);
		return new Date(seconds * 1000);
	}
	
	public static UnixDateFormatter getInstance()
	{
		return INSTANCE;
	}
	
}