package com.adviser.dhcponzoo

import java.io.Closeable
import java.io.File
import java.net.URL
import java.util.Map

import static com.google.common.base.Charsets.UTF_8

import static extension com.google.common.io.CharStreams.readLines
import static extension com.google.common.io.Files.newReader
import static extension com.google.common.io.Resources.newReaderSupplier
import static extension java.util.regex.Pattern.compile

/**
 * @author markusw
 */
class LeaseParser {

  private final val LEASE_PATTERN = 'lease\\s+(?<ip>\\d{0,3}.\\d{0,3}.\\d{0,3}.\\d{0,3})\\s+\\{(?<body>.*?)\\}'.
    compile()

  private static def <C extends Closeable, T> =>(C closebale, (C)=>T fn) {
    try {
      fn.apply(closebale)
    } finally {
      closebale.close()
    }
  }

  def Map<String, Map<String, String>> parse(URL url) {
    url.newReaderSupplier(UTF_8).input => [parse()]
  }

  def Map<String, Map<String, String>> parse(File file) {
    file.newReader(UTF_8) => [parse()]
  }

  def Map<String, Map<String, String>> parse(Readable reader) {
    val result = newHashMap

    val input = reader.readLines().map[trim].filter[!startsWith('#')].join
    val matcher = LEASE_PATTERN.matcher(input)
    while (matcher.find) {
      result.put(matcher.group('ip'), matcher.group('body').parseBody())
    }

    return result
  }

  private def Map<String, String> parseBody(String body) {
    body.split(';').toMap[split(' ', 2).get(0)].mapValues[split(' ', 2).get(1)]
  }

}
