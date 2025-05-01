export interface Game {
  id: number;
  name: string;
  active: boolean;
  minPlayers: number;
  maxPlayers: number;
  descriptions: GameDescription[];
  rules: GameRules[];
  factorySign: string;
  factoryId: number;
}

export interface GameDescription {
  id?: number;
  language: string;
  content: string;
  markdown?: boolean;
}

export interface GameRules {
  id?: number;
  language: string;
  content: string;
  markdown?: boolean;
}

export interface GameCreationDTO {
  name: string;
  active: boolean;
  minPlayers: number;
  maxPlayers: number;
  descriptions: LocalizedContent[];
  rules: LocalizedContent[];
  factorySign: string;
  factoryId: number;
}

export interface LocalizedContent {
  language: string;
  content: string;
  markdown?: boolean;
}
