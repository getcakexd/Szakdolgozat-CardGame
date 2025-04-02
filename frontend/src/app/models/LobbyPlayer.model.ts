import {User} from './user.model';

export interface LobbyPlayer {
  id: number;
  user: User;
  isLeader: boolean;
  isReady: boolean;
  joinedAt: Date;
}
