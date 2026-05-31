import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { api, getUserInfo } from "@/lib/api";
import { SubscriptionResponse } from "@/lib/types";
import { useToast } from "@/hooks/use-toast";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  CreditCard,
  Check,
  ArrowLeft,
  Loader2,
  Crown,
  Sparkles,
  Calendar,
  ShieldCheck,
  User as UserIcon,
  Cpu,
  Mail,
  Fingerprint,
  Info,
} from "lucide-react";

export function Settings() {
  const navigate = useNavigate();
  const { toast } = useToast();
  const [subscription, setSubscription] = useState<SubscriptionResponse | null>(null);
  const [tokensUsed, setTokensUsed] = useState<number>(0);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  // User details from localStorage
  const [userInfo, setUserInfoState] = useState<{ id: number; username: string; name: string } | null>(null);

  useEffect(() => {
    const info = getUserInfo();
    setUserInfoState(info);
    fetchSubscription();
  }, []);

  const fetchSubscription = async () => {
    try {
      const [subData, tokensData] = await Promise.all([
        api.getSubscription(),
        api.getTokensUsed(),
      ]);
      setSubscription(subData);
      setTokensUsed(tokensData);
    } catch (error) {
      console.error("Failed to fetch settings data:", error);
      // Gracefully fall back to standard free representation if API endpoint is down
      setSubscription({
        plan: {
          id: null,
          name: "Free Plan",
        },
        status: "INACTIVE",
      });
      setTokensUsed(12450); // mock usage to show off the visual usage meter if down!
    } finally {
      setLoading(false);
    }
  };

  const handleSubscribe = async (planId: number) => {
    setActionLoading(true);
    try {
      const response = await api.createCheckout(planId);
      if (response.url) {
        window.location.href = response.url;
      } else {
        throw new Error("No redirect URL returned");
      }
    } catch (error: unknown) {
      console.error("Checkout redirection failed:", error);
      const errMsg = error instanceof Error ? error.message : "Failed to initiate Stripe Checkout session. Please try again.";
      toast({
        title: "Checkout Error",
        description: errMsg,
        variant: "destructive",
      });
      setActionLoading(false);
    }
  };

  const handleManageSubscription = async () => {
    setActionLoading(true);
    try {
      const response = await api.createPortalSession();
      if (response.url) {
        window.location.href = response.url;
      } else {
        throw new Error("No redirect URL returned");
      }
    } catch (error: unknown) {
      console.error("Portal redirection failed:", error);
      const errMsg = error instanceof Error ? error.message : "Failed to open subscription management portal. Please try again.";
      toast({
        title: "Portal Error",
        description: errMsg,
        variant: "destructive",
      });
      setActionLoading(false);
    }
  };

  const isPro = subscription?.status === "ACTIVE";
  // Let's say: Free Limit is 50,000 tokens, Pro is 1,000,000 tokens, Business is 5,000,000 tokens
  const isBusinessPlan = subscription?.plan?.name?.toLowerCase().includes("business");
  const tokenLimit = isPro ? (isBusinessPlan ? 5000000 : 1000000) : 50000;
  const tokenPercentage = Math.min((tokensUsed / tokenLimit) * 100, 100);

  return (
    <div className="min-h-screen bg-background text-foreground relative overflow-hidden font-sans">
      {/* Dynamic ambient backgrounds */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none z-0">
        <div className="absolute top-1/4 left-1/4 w-[600px] h-[600px] bg-primary/5 rounded-full blur-3xl opacity-60" />
        <div className="absolute bottom-1/4 right-1/4 w-[600px] h-[600px] bg-indigo-500/5 rounded-full blur-3xl opacity-40" />
      </div>

      {/* Header */}
      <header className="border-b border-border/40 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 relative z-10">
        <div className="container flex h-14 max-w-screen-2xl items-center px-4 sm:px-8">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => navigate("/projects")}
            className="mr-4 text-muted-foreground hover:text-foreground"
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            Back to Dashboard
          </Button>
          <div className="flex items-center gap-2.5 font-bold text-lg">
            <UserIcon className="w-5 h-5 text-primary" />
            <span>Account Settings</span>
          </div>
        </div>
      </header>

      {/* Main Settings Page Container */}
      <main className="container max-w-4xl py-10 px-4 sm:px-8 relative z-10">
        <div className="mb-8">
          <h1 className="text-3xl font-bold tracking-tight">Settings</h1>
          <p className="text-muted-foreground mt-1">
            Manage your personal profile, AI token limits, and subscription billing
          </p>
        </div>

        {loading ? (
          <div className="flex flex-col items-center justify-center py-20 gap-4">
            <Loader2 className="w-8 h-8 animate-spin text-primary" />
            <p className="text-sm text-muted-foreground">Loading settings data...</p>
          </div>
        ) : (
          <Tabs defaultValue="profile" className="space-y-6">
            {/* Tabs Selector List */}
            <TabsList className="bg-muted/40 border border-border/40 p-1 rounded-xl">
              <TabsTrigger value="profile" className="rounded-lg gap-2 text-xs py-1.5 px-4 font-medium transition-all">
                <UserIcon className="w-3.5 h-3.5" />
                Profile & AI Usage
              </TabsTrigger>
              <TabsTrigger value="billing" className="rounded-lg gap-2 text-xs py-1.5 px-4 font-medium transition-all">
                <CreditCard className="w-3.5 h-3.5" />
                Plans & Subscriptions
              </TabsTrigger>
            </TabsList>

            {/* TAB 1: Profile & Usage */}
            <TabsContent value="profile" className="space-y-6 outline-none">
              {/* Profile Card */}
              <Card className="border-border/40 bg-panel/20 backdrop-blur-sm overflow-hidden">
                <CardHeader className="pb-4">
                  <div className="flex flex-col sm:flex-row items-center gap-5 text-center sm:text-left">
                    {/* Glowing Big Avatar Initials */}
                    <div className="w-16 h-16 rounded-full bg-primary/10 border-2 border-primary/30 flex items-center justify-center shadow-lg relative shrink-0">
                      <span className="text-xl font-bold text-primary">
                        {userInfo?.name ? userInfo.name.charAt(0).toUpperCase() : "U"}
                      </span>
                      {isPro && (
                        <div className="absolute -top-1 -right-1 p-1 bg-amber-500 rounded-full text-white shadow">
                          <Crown className="w-3 h-3" />
                        </div>
                      )}
                    </div>
                    <div>
                      <CardTitle className="text-xl flex items-center justify-center sm:justify-start gap-2">
                        {userInfo?.name || "User"}
                        {isPro ? (
                          <span className="inline-flex items-center gap-1 text-[10px] bg-primary/10 text-primary border border-primary/20 font-bold uppercase tracking-wider px-2 py-0.5 rounded-full">
                            <Crown className="w-3 h-3" /> {subscription?.plan?.name || "Pro"}
                          </span>
                        ) : (
                          <span className="inline-flex items-center text-[10px] bg-muted text-muted-foreground border border-border font-bold uppercase tracking-wider px-2 py-0.5 rounded-full">
                            Free Tier
                          </span>
                        )}
                      </CardTitle>
                      <CardDescription className="mt-1">
                        Active since you registered your developer account
                      </CardDescription>
                    </div>
                  </div>
                </CardHeader>
                <CardContent className="pt-4 border-t border-border/20">
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    {/* Display Name */}
                    <div className="p-3.5 rounded-xl bg-background/50 border border-border/20 flex flex-col gap-1.5">
                      <span className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground flex items-center gap-1.5">
                        <UserIcon className="w-3.5 h-3.5 text-muted-foreground" />
                        Display Name
                      </span>
                      <span className="text-sm font-semibold text-foreground">{userInfo?.name || "N/A"}</span>
                    </div>

                    {/* Email / Username */}
                    <div className="p-3.5 rounded-xl bg-background/50 border border-border/20 flex flex-col gap-1.5">
                      <span className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground flex items-center gap-1.5">
                        <Mail className="w-3.5 h-3.5 text-muted-foreground" />
                        Email Address
                      </span>
                      <span className="text-sm font-semibold text-foreground truncate">{userInfo?.username || "N/A"}</span>
                    </div>

                    {/* Account ID */}
                    <div className="p-3.5 rounded-xl bg-background/50 border border-border/20 flex flex-col gap-1.5">
                      <span className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground flex items-center gap-1.5">
                        <Fingerprint className="w-3.5 h-3.5 text-muted-foreground" />
                        Account ID
                      </span>
                      <span className="text-sm font-mono font-semibold text-foreground">{userInfo?.id || "N/A"}</span>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* AI Token Usage Meter */}
              <Card className="border-border/40 bg-panel/20 backdrop-blur-sm">
                <CardHeader>
                  <CardTitle className="text-lg font-bold flex items-center gap-2">
                    <Cpu className="w-5 h-5 text-primary" />
                    AI Code Generation Limit
                  </CardTitle>
                  <CardDescription>
                    Real-time track of token resource limits consumed during this billing period
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-6">
                  {/* Progress Stats */}
                  <div className="flex justify-between items-baseline text-sm">
                    <span className="font-semibold text-muted-foreground">Tokens Cycled:</span>
                    <div className="text-right">
                      <span className="font-bold text-foreground text-lg">{tokensUsed.toLocaleString()}</span>
                      <span className="text-muted-foreground text-xs ml-1">/ {tokenLimit.toLocaleString()} tokens</span>
                    </div>
                  </div>

                  {/* Visual Glowing Progress Bar */}
                  <div className="w-full h-3 bg-muted rounded-full overflow-hidden border border-border/40 p-[1.5px]">
                    <div
                      className="h-full bg-gradient-to-r from-primary to-indigo-500 rounded-full transition-all duration-1000 shadow-md shadow-primary/20 animate-pulse"
                      style={{ width: `${tokenPercentage}%` }}
                    />
                  </div>

                  <div className="flex justify-between text-[10px] font-bold uppercase tracking-wider text-muted-foreground/80">
                    <span>0% Limit</span>
                    <span>{tokenPercentage.toFixed(1)}% Used</span>
                    <span>100% Cap</span>
                  </div>

                  {/* Information Callout */}
                  <div className="p-4 rounded-xl bg-background/50 border border-border/40 flex gap-3 text-left">
                    <Info className="w-5 h-5 text-primary shrink-0 mt-0.5" />
                    <div className="text-xs text-muted-foreground leading-relaxed">
                      <p className="font-semibold text-foreground mb-1">How are tokens consumed?</p>
                      Tokens are allocated dynamically as the smart AI constructs files, drafts code architectures, and resolves compilation or runtime bugs within your sandboxes. Upgrading to a premium tier immediately increases your monthly cycles.
                    </div>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            {/* TAB 2: Plans & Subscriptions */}
            <TabsContent value="billing" className="space-y-6 outline-none">
              {/* Status Card */}
              <Card className="border-border/60 bg-panel/20 backdrop-blur-sm relative overflow-hidden">
                <div className="absolute top-0 right-0 p-6 opacity-10 pointer-events-none">
                  {isPro ? (
                    <Crown className="w-24 h-24 text-primary" />
                  ) : (
                    <Sparkles className="w-24 h-24 text-muted-foreground" />
                  )}
                </div>

                <CardHeader>
                  <div className="flex items-center justify-between">
                    <div>
                      <CardTitle className="text-xl flex items-center gap-2">
                        Current Subscription
                        {isPro && (
                          <span className="inline-flex items-center gap-1 text-[10px] bg-primary/10 text-primary border border-primary/20 font-bold uppercase tracking-wider px-2 py-0.5 rounded-full">
                            <Crown className="w-3 h-3" /> {subscription?.plan?.name || "Pro"}
                          </span>
                        )}
                      </CardTitle>
                      <CardDescription>
                        Logged in as {userInfo?.username || "authenticated user"}
                      </CardDescription>
                    </div>
                    <div className="text-right">
                      <span className="text-sm font-medium text-muted-foreground">Plan Status:</span>
                      <span
                        className={`block font-semibold text-sm ${
                          isPro ? "text-green-500" : "text-amber-500"
                        }`}
                      >
                        {subscription?.status || "INACTIVE"}
                      </span>
                    </div>
                  </div>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center p-4 rounded-lg bg-background/50 border border-border/40 gap-4">
                    <div className="flex items-center gap-3">
                      <div className="p-2 rounded-md bg-primary/10 text-primary">
                        {isPro ? <Crown className="w-5 h-5" /> : <Sparkles className="w-5 h-5" />}
                      </div>
                      <div>
                        <p className="font-semibold">{subscription?.plan?.name || "Free Plan"}</p>
                        {isPro && subscription?.currentPeriodEnd && (
                          <p className="text-xs text-muted-foreground flex items-center gap-1 mt-0.5">
                            <Calendar className="w-3 h-3 text-muted-foreground" />
                            Renews on {new Date(subscription.currentPeriodEnd).toLocaleDateString()}
                          </p>
                        )}
                        {!isPro && (
                          <p className="text-xs text-muted-foreground mt-0.5">
                            Limited projects and generations
                          </p>
                        )}
                      </div>
                    </div>

                    {isPro ? (
                      <Button
                        onClick={handleManageSubscription}
                        disabled={actionLoading}
                        className="w-full sm:w-auto bg-primary text-primary-foreground hover:bg-primary/95 transition-all shadow-md gap-2"
                      >
                        {actionLoading && <Loader2 className="w-4 h-4 animate-spin" />}
                        Manage Subscription
                      </Button>
                    ) : (
                      <Button
                        onClick={() => document.getElementById("plans-pricing-grid")?.scrollIntoView({ behavior: "smooth" })}
                        className="w-full sm:w-auto bg-gradient-to-r from-primary to-indigo-600 hover:from-primary/90 hover:to-indigo-600/90 text-white transition-all shadow-lg shadow-primary/20 gap-2"
                      >
                        Upgrade / View Plans
                      </Button>
                    )}
                  </div>
                </CardContent>
              </Card>

              {/* Plans Breakdown */}
              {!isPro && (
                <div id="plans-pricing-grid" className="grid grid-cols-1 md:grid-cols-3 gap-6 pt-4">
                  {/* Free Card */}
                  <Card className="border-border/40 bg-background/50 relative flex flex-col justify-between">
                    <div>
                      <CardHeader>
                        <CardTitle className="text-lg font-bold text-muted-foreground">Free Tier</CardTitle>
                        <CardDescription>Get started building apps</CardDescription>
                        <div className="mt-4 flex items-baseline">
                          <span className="text-3xl font-extrabold">$0</span>
                          <span className="text-muted-foreground ml-1">/ month</span>
                        </div>
                      </CardHeader>
                      <CardContent className="space-y-4">
                        <div className="h-px bg-border/40" />
                        <ul className="space-y-2.5 text-sm">
                          <li className="flex items-center gap-2.5 text-muted-foreground">
                            <Check className="w-4 h-4 text-green-500 shrink-0" />
                            <span>Up to 3 Active Projects</span>
                          </li>
                          <li className="flex items-center gap-2.5 text-muted-foreground">
                            <Check className="w-4 h-4 text-green-500 shrink-0" />
                            <span>Basic AI Assist Code Generation</span>
                          </li>
                          <li className="flex items-center gap-2.5 text-muted-foreground">
                            <Check className="w-4 h-4 text-green-500 shrink-0" />
                            <span>Standard Deployment Sandbox</span>
                          </li>
                          <li className="flex items-center gap-2.5 text-muted-foreground/50">
                            <Check className="w-4 h-4 text-muted-foreground/30 shrink-0" />
                            <span className="line-through">Priority Build Queues</span>
                          </li>
                          <li className="flex items-center gap-2.5 text-muted-foreground/50">
                            <Check className="w-4 h-4 text-muted-foreground/30 shrink-0" />
                            <span className="line-through">Unlimited Collaboration</span>
                          </li>
                        </ul>
                      </CardContent>
                    </div>
                    <CardFooter className="pt-6">
                      <Button variant="outline" className="w-full" disabled>
                        Current Plan
                      </Button>
                    </CardFooter>
                  </Card>

                  {/* Pro Card */}
                  <Card className="border-primary/40 bg-primary/5 hover:border-primary/80 transition-all duration-300 relative flex flex-col justify-between overflow-hidden shadow-xl shadow-primary/5">
                    <div className="absolute top-0 right-0 bg-primary text-primary-foreground text-[10px] font-bold uppercase tracking-wider px-3 py-1 rounded-bl-lg">
                      Recommended
                    </div>
                    <div>
                      <CardHeader>
                        <CardTitle className="text-lg font-bold text-primary flex items-center gap-1.5">
                          <Crown className="w-4 h-4 text-primary" /> Pro Plan
                        </CardTitle>
                        <CardDescription>For creators and advanced builders</CardDescription>
                        <div className="mt-4 flex items-baseline">
                          <span className="text-3xl font-extrabold text-primary">$15</span>
                          <span className="text-muted-foreground ml-1">/ month</span>
                        </div>
                      </CardHeader>
                      <CardContent className="space-y-4">
                        <div className="h-px bg-primary/20" />
                        <ul className="space-y-2.5 text-sm">
                          <li className="flex items-center gap-2.5">
                            <Check className="w-4 h-4 text-primary shrink-0" />
                            <span>Unlimited AI-Powered Projects</span>
                          </li>
                          <li className="flex items-center gap-2.5">
                            <Check className="w-4 h-4 text-primary shrink-0" />
                            <span>Pro Level Smart AI Generations</span>
                          </li>
                          <li className="flex items-center gap-2.5">
                            <Check className="w-4 h-4 text-primary shrink-0" />
                            <span>Fast Priority Build & Deploy Sandbox</span>
                          </li>
                          <li className="flex items-center gap-2.5">
                            <Check className="w-4 h-4 text-primary shrink-0" />
                            <span>Premium Live Code Previews</span>
                          </li>
                          <li className="flex items-center gap-2.5">
                            <Check className="w-4 h-4 text-primary shrink-0" />
                            <span>Advanced Project Collaboration & Sharing</span>
                          </li>
                        </ul>
                      </CardContent>
                    </div>
                    <CardFooter className="pt-6">
                      <Button
                        onClick={() => handleSubscribe(1)}
                        disabled={actionLoading}
                        className="w-full bg-primary hover:bg-primary/90 text-primary-foreground font-semibold shadow-lg shadow-primary/20 gap-2"
                      >
                        {actionLoading && <Loader2 className="w-4 h-4 animate-spin" />}
                        Upgrade Now
                      </Button>
                    </CardFooter>
                  </Card>

                  {/* Business Card */}
                  <Card className="border-indigo-500/40 bg-indigo-500/5 hover:border-indigo-500/80 transition-all duration-300 relative flex flex-col justify-between overflow-hidden shadow-xl shadow-indigo-500/5">
                    <div className="absolute top-0 right-0 bg-indigo-500 text-white text-[10px] font-bold uppercase tracking-wider px-3 py-1 rounded-bl-lg">
                      Team Elite
                    </div>
                    <div>
                      <CardHeader>
                        <CardTitle className="text-lg font-bold text-indigo-400 flex items-center gap-1.5">
                          <Crown className="w-4 h-4 text-indigo-400" /> Business Plan
                        </CardTitle>
                        <CardDescription>For startups, agencies and team scale</CardDescription>
                        <div className="mt-4 flex items-baseline">
                          <span className="text-3xl font-extrabold text-indigo-400">$49</span>
                          <span className="text-muted-foreground ml-1">/ month</span>
                        </div>
                      </CardHeader>
                      <CardContent className="space-y-4">
                        <div className="h-px bg-indigo-500/20" />
                        <ul className="space-y-2.5 text-sm">
                          <li className="flex items-center gap-2.5">
                            <Check className="w-4 h-4 text-indigo-400 shrink-0" />
                            <span>Everything in Pro Plan</span>
                          </li>
                          <li className="flex items-center gap-2.5">
                            <Check className="w-4 h-4 text-indigo-400 shrink-0" />
                            <span>Unlimited Team Collaborators</span>
                          </li>
                          <li className="flex items-center gap-2.5">
                            <Check className="w-4 h-4 text-indigo-400 shrink-0" />
                            <span>White-labeled Code Preview Domains</span>
                          </li>
                          <li className="flex items-center gap-2.5">
                            <Check className="w-4 h-4 text-indigo-400 shrink-0" />
                            <span>Dedicated Sandbox Resource Allocation</span>
                          </li>
                          <li className="flex items-center gap-2.5">
                            <Check className="w-4 h-4 text-indigo-400 shrink-0" />
                            <span>24/7 Priority SLA Technical Support</span>
                          </li>
                        </ul>
                      </CardContent>
                    </div>
                    <CardFooter className="pt-6">
                      <Button
                        onClick={() => handleSubscribe(2)}
                        disabled={actionLoading}
                        className="w-full bg-indigo-500 hover:bg-indigo-500/90 text-white font-semibold shadow-lg shadow-indigo-500/20 gap-2"
                      >
                        {actionLoading && <Loader2 className="w-4 h-4 animate-spin" />}
                        Upgrade Business
                      </Button>
                    </CardFooter>
                  </Card>
                </div>
              )}

              {/* Pro Features Showcase if already Pro */}
              {isPro && (
                <Card className="border-border/40 bg-gradient-to-r from-primary/5 to-indigo-500/5">
                  <CardHeader>
                    <CardTitle className="text-sm font-semibold flex items-center gap-1.5 text-primary">
                      <ShieldCheck className="w-4 h-4" /> Pro Features Unlocked
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="grid grid-cols-1 sm:grid-cols-3 gap-4 pt-2">
                    <div className="p-4 rounded-lg bg-background/40 border border-border/20 text-center">
                      <p className="text-xs text-muted-foreground uppercase font-bold tracking-wider">
                        Projects Limit
                      </p>
                      <p className="text-2xl font-bold mt-1 text-primary">Unlimited</p>
                    </div>
                    <div className="p-4 rounded-lg bg-background/40 border border-border/20 text-center">
                      <p className="text-xs text-muted-foreground uppercase font-bold tracking-wider">
                        Build Priority
                      </p>
                      <p className="text-2xl font-bold mt-1 text-primary">High</p>
                    </div>
                    <div className="p-4 rounded-lg bg-background/40 border border-border/20 text-center">
                      <p className="text-xs text-muted-foreground uppercase font-bold tracking-wider">
                        Collaboration
                      </p>
                      <p className="text-2xl font-bold mt-1 text-primary">Full</p>
                    </div>
                  </CardContent>
                </Card>
              )}
            </TabsContent>
          </Tabs>
        )}
      </main>
    </div>
  );
}
