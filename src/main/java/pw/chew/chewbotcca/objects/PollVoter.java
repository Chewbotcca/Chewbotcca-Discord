/*
 * Copyright (C) 2022 Chewbotcca
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package pw.chew.chewbotcca.objects;

import org.jetbrains.annotations.NotNull;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.IOException;
import java.io.Serializable;

public record PollVoter(String pollId, String userId, int choice) implements Serializable {
    public static class EntrySerializer implements Serializer<PollVoter>, Serializable {
        @Override
        public void serialize(@NotNull DataOutput2 out, @NotNull PollVoter value) throws IOException {
            out.writeUTF(value.pollId());
            out.writeUTF(value.userId());
            out.writeInt(value.choice());
        }

        @Override
        public PollVoter deserialize(@NotNull DataInput2 input, int available) throws IOException {
            return new PollVoter(input.readUTF(), input.readUTF(), input.readInt());
        }
    }
}
