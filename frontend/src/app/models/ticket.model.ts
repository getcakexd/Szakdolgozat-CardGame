export interface Ticket {
  id?: number;
  reference: string;
  name: string;
  email: string;
  subject: string;
  category: string;
  status: string;
  createdAt: Date;
  updatedAt?: Date;
  userId?: number;
  assignedToId?: number;
}

export interface TicketMessage {
  id?: number;
  ticketId: number;
  userId?: number;
  senderName: string;
  senderEmail?: string;
  senderType: 'user' | 'agent';
  message: string;
  isFromAgent?: boolean;
  createdAt: Date;
}
