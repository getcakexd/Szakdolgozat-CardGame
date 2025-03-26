import {Club} from './club.model';
import {User} from './user.model';

export interface ClubMessage {
  id: number;
  club: Club;
  sender: User;
  content: string;
  timestamp: Date;
  status: string;
}
