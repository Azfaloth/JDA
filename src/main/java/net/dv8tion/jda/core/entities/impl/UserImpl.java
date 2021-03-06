/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.core.entities.impl;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import org.json.JSONObject;

public class UserImpl implements User
{
    protected final String id;
    protected final JDAImpl api;

    protected String name;
    protected String discriminator;
    protected String avatarId;
    protected PrivateChannel privateChannel;
    protected boolean bot;
    protected boolean fake = false;

    public UserImpl(String id, JDAImpl api)
    {
        this.id = id;
        this.api = api;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getDiscriminator()
    {
        return discriminator;
    }

    @Override
    public String getAvatarId()
    {
        return avatarId;
    }

    @Override
    public String getAvatarUrl()
    {
        return getAvatarId() == null ? null : "https://cdn.discordapp.com/avatars/" + getId() + "/" + getAvatarId() + ".jpg";
    }

    @Override
    public String getDefaultAvatarId()
    {
        return DefaultAvatar.values()[Integer.parseInt(getDiscriminator()) % DefaultAvatar.values().length].toString();
    }

    @Override
    public String getDefaultAvatarUrl()
    {
        return "https://discordapp.com/assets/" + getDefaultAvatarId() + ".png";
    }

    @Override
    public boolean hasPrivateChannel()
    {
        return privateChannel != null;
    }

    @Override
    public RestAction<PrivateChannel> openPrivateChannel()
    {
        if (privateChannel != null)
            return new RestAction.EmptyRestAction<PrivateChannel>(privateChannel);

        if (fake)
            throw new IllegalStateException("Cannot open a PrivateChannel with a Fake user.");

        Route.CompiledRoute route = Route.Self.CREATE_PRIVATE_CHANNEL.compile();
        JSONObject body = new JSONObject().put("recipient_id", getId());
        return new RestAction<PrivateChannel>(api, route, body)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                {
                    PrivateChannel priv = EntityBuilder.get(api).createPrivateChannel(response.getObject());
                    UserImpl.this.privateChannel = priv;
                    request.onSuccess(priv);
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }

    @Override
    public PrivateChannel getPrivateChannel()
    {
        if (!hasPrivateChannel())
            throw new IllegalStateException("There is no PrivateChannel for this user yet! Use User#openPrivateChannel() first!");

        return privateChannel;
    }

    @Override
    public boolean isBot()
    {
        return bot;
    }

    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Override
    public String getAsMention()
    {
        return "<@" + getId() + '>';
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public boolean isFake()
    {
        return fake;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof User))
            return false;
        User oUser = (User) o;
        return this == oUser || this.getId().equals(oUser.getId());
    }

    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }

    @Override
    public String toString()
    {
        return "U:" + getName() + '(' + getId() + ')';
    }

    // -- Setters --

    public UserImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public UserImpl setDiscriminator(String discriminator)
    {
        this.discriminator = discriminator;
        return this;
    }

    public UserImpl setAvatarId(String avatarId)
    {
        this.avatarId = avatarId;
        return this;
    }

    public UserImpl setPrivateChannel(PrivateChannel privateChannel)
    {
        this.privateChannel = privateChannel;
        return this;
    }

    public UserImpl setBot(boolean bot)
    {
        this.bot = bot;
        return this;
    }

    public UserImpl setFake(boolean fake)
    {
        this.fake = fake;
        return this;
    }

    private enum DefaultAvatar
    {
        BLURPLE("6debd47ed13483642cf09e832ed0bc1b"),
        GREY("322c936a8c8be1b803cd94861bdfa868"),
        GREEN("dd4dbc0016779df1378e7812eabaa04d"),
        ORANGE("0e291f67c9274a1abdddeb3fd919cbaa"),
        RED("1cbd08c76f8af6dddce02c5138971129");

        private final String text;

        DefaultAvatar(String text)
        {
            this.text = text;
        }

        @Override
        public String toString()
        {
            return text;
        }
    }
}
