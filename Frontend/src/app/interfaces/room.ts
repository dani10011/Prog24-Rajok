export interface Room {
  id: number;
  building_Id: number;
  room_Number: string;
  capacity: number;
  is_Computer_Room: boolean;
  building?: any;
  courses?: any[];
  reservations?: any[];
  roomEntryRequests?: any[];
}

export interface RoomsResponse {
  rooms?: Room[];
}
