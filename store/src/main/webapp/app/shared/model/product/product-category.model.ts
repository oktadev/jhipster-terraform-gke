export interface IProductCategory {
  id?: number;
  name?: string;
  description?: string | null;
}

export const defaultValue: Readonly<IProductCategory> = {};
