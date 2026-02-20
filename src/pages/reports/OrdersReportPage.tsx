import { SectionTitle } from "@/components/SectionTitle";
import { Loading } from "@/components/Loading";
import { ErrorState } from "@/components/ErrorState";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/Badge";
import { useOrdersReportController } from "@/pages/reports/useOrdersReportController";
import { toReportRows } from "@/domain/reportHelpers";

/**
 * Página de reporte de órdenes.
 * Muestra todas las órdenes en formato de tabla.
 */
export function OrdersReportPage() {
  const { initialLoading, orders, error, reload } = useOrdersReportController();

  if (initialLoading) {
    return (
      <div className="container mx-auto max-w-7xl p-4">
        <SectionTitle title="Reporte de Órdenes" />
        <Loading label="Cargando órdenes..." />
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto max-w-7xl p-4">
        <SectionTitle title="Reporte de Órdenes" />
        <ErrorState title="Error al cargar órdenes" detail={error} onRetry={reload} />
      </div>
    );
  }

  if (orders.length === 0) {
    return (
      <div className="container mx-auto max-w-7xl p-4">
        <SectionTitle title="Reporte de Órdenes" />
        <Card className="p-6">
          <div className="text-center text-muted-foreground">
            No hay órdenes para mostrar
          </div>
        </Card>
      </div>
    );
  }

  const rows = toReportRows(orders);

  return (
    <div className="container mx-auto max-w-7xl p-4">
      <SectionTitle title="Reporte de Órdenes" />
      <Card className="overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full border-collapse">
            <thead>
              <tr className="border-b bg-muted/50">
                <th className="p-3 text-left text-sm font-semibold">ID Orden</th>
                <th className="p-3 text-left text-sm font-semibold">Mesa</th>
                <th className="p-3 text-left text-sm font-semibold">Estado</th>
                <th className="p-3 text-left text-sm font-semibold">Items</th>
                <th className="p-3 text-left text-sm font-semibold">Fecha</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((row) => (
                <tr key={row.id} className="border-b last:border-0 hover:bg-muted/30">
                  <td className="p-3 text-sm font-mono">{row.id}</td>
                  <td className="p-3 text-sm">{row.tableId}</td>
                  <td className="p-3 text-sm">
                    <Badge>{row.statusLabel}</Badge>
                  </td>
                  <td className="p-3 text-sm">{row.itemCount}</td>
                  <td className="p-3 text-sm">{row.createdAt}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
}
