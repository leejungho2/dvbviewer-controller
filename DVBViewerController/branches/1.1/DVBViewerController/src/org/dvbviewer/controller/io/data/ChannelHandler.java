/*
 * Copyright � 2013 dvbviewer-controller Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dvbviewer.controller.io.data;

import java.util.ArrayList;
import java.util.List;

import org.dvbviewer.controller.entities.Channel;
import org.dvbviewer.controller.entities.ChannelGroup;
import org.dvbviewer.controller.entities.ChannelRoot;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Log;
import android.util.Xml;

/**
 * The Class ChannelHandler.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class ChannelHandler extends DefaultHandler {

	List<ChannelRoot>	rootElements;
	ChannelRoot			currentRoot;
	ChannelGroup		currentGroup;
	Channel				currentChannel	= null;

	/**
	 * Parses the.
	 *
	 * @param xml the xml
	 * @return the list�
	 * @author RayBa
	 * @throws SAXException 
	 * @date 07.04.2013
	 */
	public List<ChannelRoot> parse(String xml) throws SAXException {

		RootElement channels = new RootElement("channels");
		Element rootElement = channels.getChild("root");
		Element groupElement = rootElement.getChild("group");
		Element channelElement = groupElement.getChild("channel");
		Element subChanElement = channelElement.getChild("subchannel");
		Element logoElement = channelElement.getChild("logo");

		channels.setStartElementListener(new StartElementListener() {

			@Override
			public void start(Attributes attributes) {
				rootElements = new ArrayList<ChannelRoot>();
			}
		});

		rootElement.setStartElementListener(new StartElementListener() {
			public void start(Attributes attributes) {
				currentRoot = new ChannelRoot();
				currentRoot.setName(attributes.getValue("name"));
				rootElements.add(currentRoot);
			}
		});

		groupElement.setStartElementListener(new StartElementListener() {
			public void start(Attributes attributes) {
				currentGroup = new ChannelGroup();
				currentGroup.setName(attributes.getValue("name"));
				currentRoot.getGroups().add(currentGroup);
			}
		});

		channelElement.setStartElementListener(new StartElementListener() {
			public void start(Attributes attributes) {
				currentChannel = new Channel();
				currentChannel.setChannelID(Long.valueOf(attributes.getValue("ID")));
				currentChannel.setPosition(Integer.valueOf(attributes.getValue("nr")));
				currentChannel.setName(attributes.getValue("name"));
				currentChannel.setEpgID(Long.valueOf(attributes.getValue("EPGID")));
				currentGroup.getChannels().add(currentChannel);
			}
		});

		logoElement.setEndTextElementListener(new EndTextElementListener() {

			@Override
			public void end(String body) {
				currentChannel.setLogoUrl(body);
			}

		});

		subChanElement.setStartElementListener(new StartElementListener() {
			public void start(Attributes attributes) {
				Channel c = new Channel();
				c.setChannelID(Long.valueOf(attributes.getValue("ID")));
				c.setPosition(currentChannel.getPosition());
				c.setName(attributes.getValue("name"));
				c.setEpgID(currentChannel.getEpgID());
				c.setLogoUrl(currentChannel.getLogoUrl());
				c.setFlag(Channel.FLAG_ADDITIONAL_AUDIO);
				currentGroup.getChannels().add(c);
			}
		});

		Xml.parse(xml, channels.getContentHandler());
		return rootElements;

	}

}
