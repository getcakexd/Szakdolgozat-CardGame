import {Club} from './club.model';
import {User} from './user.model';

export interface ClubInvite {
  id: number;
  club: Club;
  user: User;
  status: string;
  clubName: string;
  username: string;
}
