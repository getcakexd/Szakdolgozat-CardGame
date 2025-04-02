import {User} from './user.model';

export interface LobbyMessage {
  id: number;
  user: User;
  content: string;
  sentAt: Date;
}
