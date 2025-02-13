/*
 * Nextcloud Talk application
 *  
 * @author Tobias Kaminsky
 * Copyright (C) 2020 Tobias Kaminsky <tobias.kaminsky@nextcloud.com>
 *  
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextcloud.talk.adapters.messages;

import com.nextcloud.talk.chat.ChatActivity;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.ViewHolder;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.List;

public class TalkMessagesListAdapter<M extends IMessage> extends MessagesListAdapter<M> {
    private final ChatActivity chatActivity;

    public TalkMessagesListAdapter(
        String senderId,
        MessageHolders holders,
        ImageLoader imageLoader,
        ChatActivity chatActivity) {
        super(senderId, holders, imageLoader);
        this.chatActivity = chatActivity;
    }
    
    public List<MessagesListAdapter.Wrapper> getItems() {
        return items;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        if (holder instanceof IncomingTextMessageViewHolder) {
            ((IncomingTextMessageViewHolder) holder).assignCommonMessageInterface(chatActivity);
        } else if (holder instanceof OutcomingTextMessageViewHolder) {
            ((OutcomingTextMessageViewHolder) holder).assignCommonMessageInterface(chatActivity);

        } else if (holder instanceof IncomingLocationMessageViewHolder) {
            ((IncomingLocationMessageViewHolder) holder).assignCommonMessageInterface(chatActivity);
        } else if (holder instanceof OutcomingLocationMessageViewHolder) {
            ((OutcomingLocationMessageViewHolder) holder).assignCommonMessageInterface(chatActivity);

        } else if (holder instanceof IncomingLinkPreviewMessageViewHolder) {
            ((IncomingLinkPreviewMessageViewHolder) holder).assignCommonMessageInterface(chatActivity);
        } else if (holder instanceof OutcomingLinkPreviewMessageViewHolder) {
            ((OutcomingLinkPreviewMessageViewHolder) holder).assignCommonMessageInterface(chatActivity);

        } else if (holder instanceof IncomingVoiceMessageViewHolder) {
            ((IncomingVoiceMessageViewHolder) holder).assignVoiceMessageInterface(chatActivity);
            ((IncomingVoiceMessageViewHolder) holder).assignCommonMessageInterface(chatActivity);
        } else if (holder instanceof OutcomingVoiceMessageViewHolder) {
            ((OutcomingVoiceMessageViewHolder) holder).assignVoiceMessageInterface(chatActivity);
            ((OutcomingVoiceMessageViewHolder) holder).assignCommonMessageInterface(chatActivity);

        } else if (holder instanceof PreviewMessageViewHolder) {
            ((PreviewMessageViewHolder) holder).assignPreviewMessageInterface(chatActivity);
            ((PreviewMessageViewHolder) holder).assignCommonMessageInterface(chatActivity);
        }
    }
}
