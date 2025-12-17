import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";

const inter = Inter({
  subsets: ["latin", "cyrillic"],
  variable: "--font-inter",
  display: "swap",
});

export const metadata: Metadata = {
  metadataBase: new URL("https://z-bank.com"),
  title: {
    default: "Z-Bank — Современный цифровой банк | by Никита Фесенко",
    template: "%s | Z-Bank",
  },
  description:
    "Z-Bank — инновационный цифровой банк с мгновенными переводами, надежной защитой и современным интерфейсом. Разработано Никитой Фесенко.",
  keywords: [
    "банк",
    "цифровой банк",
    "онлайн банкинг",
    "переводы",
    "финтех",
    "Z-Bank",
    "Никита Фесенко",
  ],
  authors: [{ name: "Никита Фесенко" }],
  creator: "Никита Фесенко",
  publisher: "Z-Bank",
  robots: {
    index: true,
    follow: true,
    googleBot: {
      index: true,
      follow: true,
      "max-video-preview": -1,
      "max-image-preview": "large",
      "max-snippet": -1,
    },
  },
  openGraph: {
    type: "website",
    locale: "ru_RU",
    url: "https://z-bank.com",
    siteName: "Z-Bank",
    title: "Z-Bank — Современный цифровой банк",
    description:
      "Инновационный цифровой банк с мгновенными переводами и надежной защитой. Разработано Никитой Фесенко.",
    images: [
      {
        url: "/og-image.png",
        width: 1200,
        height: 630,
        alt: "Z-Bank — Цифровой банк нового поколения",
      },
    ],
  },
  twitter: {
    card: "summary_large_image",
    title: "Z-Bank — Современный цифровой банк",
    description:
      "Инновационный цифровой банк с мгновенными переводами и надежной защитой.",
    images: ["/og-image.png"],
    creator: "@zbank",
  },
  icons: {
    icon: "/favicon.ico",
    shortcut: "/favicon-16x16.png",
    apple: "/apple-touch-icon.png",
  },
  manifest: "/site.webmanifest",
  alternates: {
    canonical: "https://z-bank.com",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ru" className="dark">
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
        {/* JSON-LD Structured Data */}
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{
            __html: JSON.stringify({
              "@context": "https://schema.org",
              "@type": "FinancialService",
              name: "Z-Bank",
              description: "Современный цифровой банк с мгновенными переводами",
              url: "https://z-bank.com",
              logo: "https://z-bank.com/logo.png",
              founder: {
                "@type": "Person",
                name: "Никита Фесенко",
              },
              areaServed: "Worldwide",
              serviceType: "Digital Banking",
            }),
          }}
        />
      </head>
      <body className={`${inter.variable} font-sans antialiased`}>
        {children}
      </body>
    </html>
  );
}
