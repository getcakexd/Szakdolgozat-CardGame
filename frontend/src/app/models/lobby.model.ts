import {LobbyPlayer} from './LobbyPlayer.model';
import {LobbyMessage} from './LobbyMessage.model';

export interface Lobby {
  id: number;
  code: string;
  name: string;
  isPublic: boolean;
  withPoints: boolean;
  gameMode: string;
  maxPlayers: number;
  players: LobbyPlayer[];
  messages: LobbyMessage[];
  createdAt: Date;
}
