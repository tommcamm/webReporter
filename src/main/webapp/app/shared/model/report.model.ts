import { IUser } from 'app/shared/model/user.model';

export interface IReport {
  id?: number;
  title?: string;
  description?: string | null;
  ipAddress?: string;
  user?: IUser;
}

export const defaultValue: Readonly<IReport> = {};
