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

public enum DateFormatType {
	/**
	 * Use custom {@link java.text.DateFormat} to format Date at serialization if
	 * was set. Otherwise is same as {@code DateFormatType.DEFAULT}
	 */
	CUSTOM,
	/**
	 * Format dates using EN-US format.
	 */
	EN_US,
	/**
	 * Format dates with local format.
	 */
	LOCAL,
	/**
	 * Format dates with ISO8601 format ( yyyy-MM-dd'T'HH:mm:ss.SSS'Z' )
	 */
	ISO_8601,
	/**
	 * Format dates as milliseconds
	 */
	MILLIS,
	/**
	 * Format dates as seconds
	 */
	UNIX,
	/**
	 * Use default: EN-US.
	 */
	DEFAULT;
}
