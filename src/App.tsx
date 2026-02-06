import { Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout } from "./components/AppLayout";
import { TableSelectPage } from '@/pages/client/TableSelectPage'
import { MenuPage } from '@/pages/client/MenuPage'
import { CartPage } from '@/pages/client/CartPage'
import { ConfirmationPage } from '@/pages/client/ConfirmationPage'
import { OrderStatusPage } from '@/pages/client/OrderStatusPage'
import { KitchenLoginPage } from '@/pages/kitchen/KitchenLoginPage'
import { KitchenBoardPage } from '@/pages/kitchen/KitchenBoardPage'
import { CartProvider } from '@/store/cart'

export default function App() {
  return (
    <CartProvider>
      <Routes>
        <Route element={<AppLayout />}>
          <Route path="/" element={<Navigate to="/client/table" replace />} />

          <Route path="/client/table" element={<TableSelectPage />} />
          <Route path="/client/menu" element={<MenuPage />} />
          <Route path="/client/cart" element={<CartPage />} />
          <Route path="/client/confirm/:orderId" element={<ConfirmationPage />} />
          <Route path="/client/status" element={<OrderStatusPage />} />
          <Route path="/client/status/:orderId" element={<OrderStatusPage />} />

          <Route path="/kitchen" element={<KitchenLoginPage />} />
          <Route path="/kitchen/board" element={<KitchenBoardPage />} />

          <Route path="*" element={<Navigate to="/client/table" replace />} />
        </Route>
      </Routes>
    </CartProvider>
  )
}
