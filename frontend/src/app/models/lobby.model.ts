import { User } from './user.model';
import { Game } from './game.model';

export const LOBBY_STATUS = {
  WAITING: 'WAITING',
  IN_GAME: 'IN_GAME',
  FINISHED: 'FINISHED'
};

export interface Lobby {
  id: number;
  code: string;
  leader: User;
  game: Game;
  playWithPoints: boolean;
  minPlayers: number;
  players: User[];
  status: string;
}
