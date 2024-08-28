import dayjs from 'dayjs';
import { OrderStatus } from 'app/shared/model/enumerations/order-status.model';

export interface IProductOrder {
  id?: number;
  placedDate?: dayjs.Dayjs;
  status?: keyof typeof OrderStatus;
  code?: string;
  invoiceId?: number | null;
  customer?: string;
}

export const defaultValue: Readonly<IProductOrder> = {};
