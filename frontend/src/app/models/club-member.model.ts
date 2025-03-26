import {Club} from './club.model';
import {User} from './user.model';

export interface ClubMember{
  id: number;
  club: Club;
  user: User;
  role: string;
  username: string;
}
