import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { XCircle, ArrowLeft, ArrowRight, ShieldAlert } from "lucide-react";

export function PaymentCancel() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-background flex flex-col items-center justify-center relative overflow-hidden px-4">
      {/* Dynamic ambient backgrounds */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-primary/5 rounded-full blur-3xl" />
        <div className="absolute top-1/4 right-1/4 w-[300px] h-[300px] bg-amber-500/5 rounded-full blur-2xl" />
      </div>

      <Card className="w-full max-w-md border-amber-500/20 bg-background/70 backdrop-blur-md shadow-2xl relative z-10 overflow-hidden">
        {/* Amber visual cancellation line */}
        <div className="h-1.5 w-full bg-gradient-to-r from-amber-500 to-orange-400" />

        <CardHeader className="text-center pt-8">
          <div className="mx-auto w-16 h-16 bg-amber-500/10 rounded-full flex items-center justify-center text-amber-500 mb-4 animate-pulse">
            <XCircle className="w-10 h-10" />
          </div>
          <CardTitle className="text-2xl font-bold tracking-tight text-amber-500">
            Checkout Cancelled
          </CardTitle>
          <CardDescription className="mt-1">
            You have returned from Stripe Checkout
          </CardDescription>
        </CardHeader>

        <CardContent className="space-y-4 px-6 text-center">
          <div className="p-4 rounded-lg bg-amber-500/5 border border-amber-500/10 space-y-3">
            <div className="flex items-start gap-2.5 text-left text-sm text-amber-500/90">
              <ShieldAlert className="w-5 h-5 shrink-0 mt-0.5" />
              <p className="leading-relaxed">
                No money was charged, and your payment details were not processed. Your current account plan remains fully active without any changes.
              </p>
            </div>
          </div>
          <p className="text-sm text-muted-foreground pt-2">
            If you changed your mind or would like to choose a different billing plan, you can review the subscription benefits at any time.
          </p>
        </CardContent>

        <CardFooter className="p-6 bg-muted/20 border-t border-border/40 flex flex-col sm:flex-row gap-3">
          <Button
            variant="outline"
            onClick={() => navigate("/settings")}
            className="w-full gap-2"
          >
            <ArrowLeft className="w-4 h-4" />
            View Plans Again
          </Button>
          <Button
            onClick={() => navigate("/projects")}
            className="w-full bg-primary hover:bg-primary/95 gap-2"
          >
            Return to Dashboard
            <ArrowRight className="w-4 h-4" />
          </Button>
        </CardFooter>
      </Card>
    </div>
  );
}
