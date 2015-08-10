package com.yabinc.networkusage;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yabinc on 7/18/15.
 */
/*
<feed xmlns="http://www.w3.org/2005/Atom" xmlns:creativeCommons="http://backend.userland.com/creativeCommonsRssModule" xmlns:re="http://purl.org/atompub/rank/1.0">
    <title type="text">Active questions tagged android - Stack Overflow</title>
    <link rel="self" href="http://stackoverflow.com/feeds/tag?tagnames=android&amp;sort=newest&quot;" type="application/atom+xml" />
    <link rel="alternate" href="http://stackoverflow.com/questions/tagged/?tagnames=android&amp;sort=active" type="text/html" />
    <subtitle>most recent 30 from stackoverflow.com</subtitle>
    <updated>2015-07-19T00:42:01Z</updated>
    <id>http://stackoverflow.com/feeds/tag?tagnames=android&amp;sort=newest&quot;</id>
    <creativeCommons:license>http://www.creativecommons.org/licenses/by-sa/3.0/rdf</creativeCommons:license>
    <entry>
        <id>http://stackoverflow.com/q/31489374</id>
        <re:rank scheme="http://stackoverflow.com">0</re:rank>
        <title type="text">nullPointer exception when trying to access adapter after asynctask refresh</title>
            <category scheme="http://stackoverflow.com/tags" term="java" />
            <category scheme="http://stackoverflow.com/tags" term="android" />
            <category scheme="http://stackoverflow.com/tags" term="listview" />
            <category scheme="http://stackoverflow.com/tags" term="android-asynctask" />
            <category scheme="http://stackoverflow.com/tags" term="android-arrayadapter" />
        <author>
            <name>John Park</name>
            <uri>http://stackoverflow.com/users/2272467</uri>
        </author>
        <link rel="alternate" href="http://stackoverflow.com/questions/31489374/nullpointer-exception-when-trying-to-access-adapter-after-asynctask-refresh" />
        <published>2015-07-18T08:40:29Z</published>
        <updated>2015-07-19T00:35:23Z</updated>
        <summary type="html">
 */
public class StackoverflowXmlParser {
    public class Entry {
        public String title;
        public String link;
        public String summary;

        Entry(String title, String summary, String link) {
            this.title = title;
            this.summary = summary;
            this.link = link;
        }
    }

    public List<Entry> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private List<Entry> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Entry> entries = new ArrayList<Entry>();

        parser.require(XmlPullParser.START_TAG, null, "feed");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("entry")) {
                entries.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    private Entry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "entry");
        String title = null;
        String summary = null;
        String link = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("title")) {
                title = readTitle(parser);
            } else if (name.equals("summary")) {
                summary = readSummary(parser);
            } else if (name.equals("link")) {
                link = readLink(parser);
            } else {
                skip(parser);
            }
        }
        return new Entry(title, summary, link);
    }

    private String readTitle(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "title");
        return title;
    }

    private String readText(XmlPullParser parser) throws XmlPullParserException, IOException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private String readSummary(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "summary");
        String summary = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "summary");
        return summary;
    }

    private String readLink(XmlPullParser parser) throws XmlPullParserException, IOException {
        String link = "";
        parser.require(XmlPullParser.START_TAG, null, "link");
        String tag = parser.getName();
        String relType = parser.getAttributeValue(null, "rel");
        if (relType.equals("alternate")) {
            link = parser.getAttributeValue(null, "href");
            parser.nextTag();
        }
        parser.require(XmlPullParser.END_TAG, null, "link");
        return link;
    }

    void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
