export interface Game {
  id: number;
  name: string;
  description: string;
  active: boolean;
  minPlayers: number;
  maxPlayers: number;
}
