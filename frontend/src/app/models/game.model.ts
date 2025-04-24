export interface Game {
  id: number;
  name: string;
  active: boolean;
  minPlayers: number;
  maxPlayers: number;
  descriptions: GameDescription[];
  rules: GameRules[];
}

export interface GameDescription {
  id?: number;
  language: string;
  content: string;
}

export interface GameRules {
  id?: number;
  language: string;
  content: string;
}

export interface GameCreationDTO {
  name: string;
  active: boolean;
  minPlayers: number;
  maxPlayers: number;
  descriptions: LocalizedContent[];
  rules: LocalizedContent[];
}

export interface LocalizedContent {
  language: string;
  content: string;
}
