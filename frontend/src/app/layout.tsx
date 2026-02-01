import type { Metadata } from "next";
import { headers } from "next/headers";
import { JetBrains_Mono, Noto_Sans_SC } from "next/font/google";
import { Toaster } from "sonner";
import { AuthProvider } from "../lib/auth/AuthProvider";
import { Navbar } from "../components/Navbar";
import "./globals.css";

const notoSansSc = Noto_Sans_SC({
  variable: "--font-sans",
  subsets: ["latin"],
  weight: ["400", "500", "600", "700"],
  display: "swap",
});

const jetbrainsMono = JetBrains_Mono({
  variable: "--font-mono",
  subsets: ["latin"],
  display: "swap",
});

export const metadata: Metadata = {
  title: "EBlog",
  description: "面向开发者的多用户技术博客平台",
};

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  // 服务端预判：读取 Cookie 判断是否可能已登录
  const headersList = await headers();
  const cookieHeader = headersList.get("cookie") || "";
  const hasRefreshToken = cookieHeader.includes("refresh_token=");

  return (
    <html lang="zh-CN">
      <body className={`${notoSansSc.variable} ${jetbrainsMono.variable} antialiased min-h-screen bg-background text-foreground`}>
        <AuthProvider initialAuth={hasRefreshToken}>
          <Navbar />
          {children}
          <Toaster richColors position="top-center" />
        </AuthProvider>
      </body>
    </html>
  );
}
