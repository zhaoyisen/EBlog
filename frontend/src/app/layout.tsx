import type { Metadata } from "next";
import { JetBrains_Mono, Noto_Sans_SC } from "next/font/google";
import { AuthProvider } from "../lib/auth/AuthProvider";
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

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="zh-CN">
      <body className={`${notoSansSc.variable} ${jetbrainsMono.variable} antialiased`}>
        <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  );
}
