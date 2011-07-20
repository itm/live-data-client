/**
 * Copyright (C) 2011 Universität zu Lübeck, Institut für Telematik (ITM)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.smartsantander.livedataclient.util

import java.util.Collection

/**
 * Utility class for common pre- and post-condition checks.
 * <p/>
 * The class provides two kinds of checks: Argument checks and general checks.
 * The difference is, that argument checks throw an {@link IllegalArgumentException},
 * while general checks throw a {@link RuntimeException}.
 *
 * @author Soenke Nommensen
 */
object Checks {

  /**
   * @param expression Boolean expression, which shall be tested.
   * @param message    Error message
   * @throws IllegalArgumentException
   */
  def checkArgument(expression: Boolean, message: String) {
    if (!expression) {
      throw new IllegalArgumentException(message)
    }
  }

  /**
   * @param argument Object reference, which shall be tested for Null.
   * @param message  Error message
   */
  def ifNullArgument(argument: AnyRef, message: String) {
    checkArgument(argument != null, message)
  }

  /**
   * @param stringArgument String reference, which shall be tested for Null or empty.
   * @param message        Error message
   */
  def ifNullOrEmptyArgument(stringArgument: String, message: String) {
    checkArgument(stringArgument != null && !stringArgument.isEmpty, message)
  }

  /**
   * @param collection Collection, which shall be tested for Null or empty.
   * @param message    Error message
   * @throws RuntimeException
   */
  @SuppressWarnings(Array("rawtypes")) def ifNullOrEmptyArgument(collection: Collection[_], message: String) {
    checkArgument(collection != null && !collection.isEmpty, message)
  }

  /**
   * @param expression Boolean expression, which shall be tested.
   * @param message    Error message
   * @throws RuntimeException
   */
  def check(expression: Boolean, message: String) {
    if (!expression) {
      throw new RuntimeException(message)
    }
  }

  /**
   * @param reference Object reference, which shall be tested for Null.
   * @param message   Error message
   * @throws RuntimeException
   */
  def ifNull(reference: AnyRef, message: String) {
    check(reference != null, message)
  }

  /**
   * @param stringReference String reference, which shall be tested for Null or empty.
   * @param message         Error message
   * @throws RuntimeException
   */
  def ifNullOrEmpty(stringReference: String, message: String) {
    check(stringReference != null && !stringReference.isEmpty, message)
  }

  /**
   * @param collection Collection, which shall be tested for Null or empty.
   * @param message    Error message
   * @throws RuntimeException
   */
  def ifNullOrEmpty(collection: Collection[_], message: String) {
    check(collection != null && !collection.isEmpty, message)
  }
}