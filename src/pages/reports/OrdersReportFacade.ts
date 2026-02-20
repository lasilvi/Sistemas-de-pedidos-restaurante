import type { Order } from "@/api/contracts";
import { listOrders } from "@/api/orders";

/**
 * Facade para el reporte de órdenes.
 * Orquesta la carga de todas las órdenes sin filtro de estado.
 */
export class OrdersReportFacade {
  /**
   * Carga todas las órdenes desde el API.
   * @returns Promise con array de órdenes (puede ser vacío)
   * @throws Error si falla la llamada al API
   */
  async fetchAllOrders(): Promise<Order[]> {
    return await listOrders({});
  }
}
