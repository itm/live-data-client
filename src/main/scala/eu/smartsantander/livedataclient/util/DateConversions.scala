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

import javax.xml.datatype.{DatatypeFactory, XMLGregorianCalendar}

/**
 *
 * @author Soenke Nommensen 
 */

object DateConversions {

  implicit def date2xmlGregorianCalendar(date: java.util.Date): XMLGregorianCalendar = {
    val gregorianCalendar = new java.util.GregorianCalendar
    gregorianCalendar.setTime(date)

    DatatypeFactory.newInstance.newXMLGregorianCalendar(gregorianCalendar)
  }

  implicit def xmlGregorianCalendar2long(xgc: XMLGregorianCalendar): java.lang.Long = {
    xgc.toGregorianCalendar.getTime
  }

  implicit def long2xmlGregorianCalendar(long: java.lang.Long): XMLGregorianCalendar = {
    val gc = new java.util.GregorianCalendar
    gc.setTime(long)

    DatatypeFactory.newInstance.newXMLGregorianCalendar(gc)
  }

  implicit def date2long(date: java.util.Date): java.lang.Long = {
    new java.lang.Long(date.getTime)
  }

  implicit def long2date(long: java.lang.Long): java.util.Date = {
    new java.util.Date(long)
  }
}