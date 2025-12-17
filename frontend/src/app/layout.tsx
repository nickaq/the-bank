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
    default: "Z-Bank — Modern Digital Banking | by Moritz Müller",
    template: "%s | Z-Bank",
  },
  description:
    "Z-Bank is an innovative digital bank with instant transfers, robust security, and a modern interface. Developed by Moritz Müller.",
  keywords: [
    "bank",
    "digital bank",
    "online banking",
    "transfers",
    "fintech",
    "Z-Bank",
    "Moritz Müller",
  ],
  authors: [{ name: "Moritz Müller" }],
  creator: "Moritz Müller",
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
    locale: "en_US",
    url: "https://z-bank.com",
    siteName: "Z-Bank",
    title: "Z-Bank — Modern Digital Banking",
    description:
      "Innovative digital bank with instant transfers and robust security. Developed by Moritz Müller.",
    images: [
      {
        url: "/og-image.png",
        width: 1200,
        height: 630,
        alt: "Z-Bank — Next Generation Digital Banking",
      },
    ],
  },
  twitter: {
    card: "summary_large_image",
    title: "Z-Bank — Modern Digital Banking",
    description:
      "Innovative digital bank with instant transfers and robust security.",
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
    <html lang="en" className="dark">
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
              description: "Modern digital bank with instant transfers",
              url: "https://z-bank.com",
              logo: "https://z-bank.com/logo.png",
              founder: {
                "@type": "Person",
                name: "Moritz Müller",
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
