import {User} from './user.model';

export interface FriendRequest {
  id: number;
  sender: User;
  receiver: User;
  status: string;
}
