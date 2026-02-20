import { describe, it, expect, vi, beforeEach } from "vitest";
import { OrdersReportFacade } from "@/pages/reports/OrdersReportFacade";
import * as ordersApi from "@/api/orders";
import type { Order } from "@/api/contracts";

// Mock del API
vi.mock("@/api/orders", () => ({
  listOrders: vi.fn(),
}));

// ════════════════════════════════════════════════════════════════════════════
// Fixtures
// ════════════════════════════════════════════════════════════════════════════

const MOCK_ORDERS: Order[] = [
  {
    id: "order-001",
    tableId: 3,
    status: "PENDING",
    items: [{ productId: 1, quantity: 2, name: "Empanadas" }],
    createdAt: "2026-02-19T10:00:00Z",
  },
];

// ════════════════════════════════════════════════════════════════════════════
// Tests
// ════════════════════════════════════════════════════════════════════════════

describe("OrdersReportFacade", () => {
  let facade: OrdersReportFacade;

  beforeEach(() => {
    vi.clearAllMocks();
    facade = new OrdersReportFacade();
  });

  it("fetchAllOrders() retorna orders desde el API", async () => {
    vi.mocked(ordersApi.listOrders).mockResolvedValue(MOCK_ORDERS);

    const result = await facade.fetchAllOrders();

    expect(result).toEqual(MOCK_ORDERS);
    expect(ordersApi.listOrders).toHaveBeenCalledWith({});
  });

  it("fetchAllOrders() propaga errores del API", async () => {
    const errorMsg = "API error";
    vi.mocked(ordersApi.listOrders).mockRejectedValue(new Error(errorMsg));

    await expect(facade.fetchAllOrders()).rejects.toThrow(errorMsg);
  });

  it("fetchAllOrders() retorna array vacío sin lanzar error", async () => {
    vi.mocked(ordersApi.listOrders).mockResolvedValue([]);

    const result = await facade.fetchAllOrders();

    expect(result).toEqual([]);
  });
});
