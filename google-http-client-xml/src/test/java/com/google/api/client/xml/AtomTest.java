/*
 * Copyright (c) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.xml;

import static org.junit.Assert.assertNull;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.xml.atom.AtomFeedParser;
import com.google.api.client.util.Key;
import com.google.api.client.xml.atom.AbstractAtomFeedParser;
import com.google.api.client.xml.atom.Atom;

/**
 * Tests {@link Atom}.
 *
 * @author Yaniv Inbar
 */
public class AtomTest {

  private static final String SAMPLE_FEED = "<?xml version=\"1.0\" encoding=\"utf-8\"?><feed xmlns=\"http://www.w3.org/2005/Atom\">  <title>Example Feed</title>  <link " +
      "href=\"http://example.org/\"/>  <updated>2003-12-13T18:30:02Z</updated>  <author>    <name>John Doe</name>  </author>  " +
      "<id>urn:uuid:60a76c80-d399-11d9-b93C-0003939e0af6</id>  <entry>    <title>Atom-Powered Robots Run Amok</title>    <link href=\"http://example.org/2003/12/13/atom03\"/>   " +
      " <id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id>    <updated>2003-12-13T18:30:02Z</updated>    <summary>Some text.</summary>  </entry></feed>";

  @SuppressWarnings("unchecked")
  @Test
  public void testSetSlugHeader() {
    HttpHeaders headers = new HttpHeaders();
    assertNull(headers.get("Slug"));
    subtestSetSlugHeader(headers, "value", "value");
    subtestSetSlugHeader(headers, " !\"#$&'()*+,-./:;<=>?@[\\]^_`{|}~", " !\"#$&'()*+,-./:;<=>?@[\\]^_`{|}~");
    subtestSetSlugHeader(headers, "%D7%99%D7%A0%D7%99%D7%91", "יניב");
    subtestSetSlugHeader(headers, null, null);
  }

  @SuppressWarnings("unchecked")
  public void subtestSetSlugHeader(HttpHeaders headers, String expectedValue, String value) {
    Atom.setSlugHeader(headers, value);
    if (value == null) {
      assertNull(headers.get("Slug"));
    } else {
      Assert.assertArrayEquals(new String[]{expectedValue}, ((List<String>) headers.get("Slug")).toArray());
    }
  }

  @Test
  public void testAtomFeedParser() throws XmlPullParserException, IOException {
    XmlPullParser parser = Xml.createParser();
    // Wired. Both, the InputStream for the FeedParser and the XPP need to be set (?)
    parser.setInput(new StringReader(SAMPLE_FEED));
    InputStream stream = new ByteArrayInputStream(SAMPLE_FEED.getBytes());
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    AbstractAtomFeedParser atomParser = new AtomFeedParser<Feed, FeedEntry>(namespaceDictionary, parser, stream, Feed.class, FeedEntry.class);
    Object obj = atomParser.parseFeed();
    // TODO: Evaluate Result
  }


  @Test
  public void testHeiseFeedParser() throws IOException, XmlPullParserException {
    XmlPullParser parser = Xml.createParser();
    ClassLoader classLoader = getClass().getClassLoader();
    File atomFile = new File(classLoader.getResource("heise-atom.xml").getFile());
    InputStream stream = new FileInputStream(atomFile);
    Reader atomReader = new FileReader(atomFile);
    parser.setInput(atomReader);
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    AbstractAtomFeedParser atomParser = new AtomFeedParser<Feed, FeedEntry>(namespaceDictionary, parser, stream, Feed.class, FeedEntry.class);
    Object obj = atomParser.parseFeed();
    // TODO: Evaluate Result
    atomReader.close();
    stream.close();
  }

  public static class Feed {

    @Key
    private String title;

    @Key
    private Link link;

    @Key
    private String updated;

    @Key
    private Author author;

    @Key
    private String id;

    @Key
    private FeedEntry[] entry;

  }

  public static class Author {

    @Key
    private String name;
  }

  public static class Link {

    @Key("@href")
    private String href;

  }

  public static class FeedEntry {

    @Key
    private String title;

    @Key
    private Link link;

    @Key
    private String updated;

    @Key
    private String summary;

    @Key
    private String id;


  }


}
