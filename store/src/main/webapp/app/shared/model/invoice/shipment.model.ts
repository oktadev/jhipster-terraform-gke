import dayjs from 'dayjs';
import { IInvoice } from 'app/shared/model/invoice/invoice.model';

export interface IShipment {
  id?: number;
  trackingCode?: string | null;
  date?: dayjs.Dayjs;
  details?: string | null;
  invoice?: IInvoice;
}

export const defaultValue: Readonly<IShipment> = {};
